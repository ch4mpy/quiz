import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;
import 'package:http_interceptor/http_interceptor.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'http.g.dart';

class CookieInterceptor extends InterceptorContract {
  final List<Cookie> _cookies = [];

  @override
  FutureOr<http.BaseRequest> interceptRequest(
      {required http.BaseRequest request}) {
    final String cookieHeader = _cookieHeader(request.url);
    if (cookieHeader.isNotEmpty) {
      request.headers.update('cookie', (value) => "$value; $cookieHeader",
          ifAbsent: () => cookieHeader);
    }

    if (request.method == 'POST' ||
        request.method == 'PUT' ||
        request.method == 'PATCH' ||
        request.method == 'DELETE') {
      final csrfCookie = _cookies.firstWhere((cookie) {
        final domain = cookie.domain ?? '';
        return domain.isNotEmpty &&
            request.url.host.contains(domain) &&
            cookie.name == 'XSRF-TOKEN';
      }, orElse: () => Cookie('XSRF-TOKEN', ''));
      if (csrfCookie.value.isNotEmpty) {
        request.headers['X-XSRF-TOKEN'] = csrfCookie.value;
      }
    }

    return request;
  }

  @override
  FutureOr<http.BaseResponse> interceptResponse(
      {required http.BaseResponse response}) {
    _cleanExpiredCookies();

    response.headersSplitValues['set-cookie']
        ?.map(Cookie.fromSetCookieValue)
        .forEach((cookie) {
      if ((cookie.sameSite == SameSite.lax ||
              cookie.sameSite == SameSite.strict) &&
          cookie.domain == null) {
        cookie.domain = response.request!.url.host;
      }
      _setOrReplace(cookie);
    });

    return response;
  }

  void _setOrReplace(Cookie cookie) {
    _cookies
        .removeWhere((c) => c.name == cookie.name && c.domain == cookie.domain);
    _cookies.add(cookie);
  }

  String _cookieHeader(Uri uri) {
    return _getSiteCookies(uri).map((cookie) {
      return "${cookie.name}=${cookie.value}";
    }).join("; ");
  }

  Iterable<Cookie> _getSiteCookies(Uri uri) {
    return _cookies.where((cookie) {
      if (cookie.sameSite == SameSite.none || cookie.domain == null) {
        return true;
      }
      return uri.host.endsWith(cookie.domain as String);
    });
  }

  void _cleanExpiredCookies() {
    final DateTime now = DateTime.now();
    _cookies.removeWhere((cookie) {
      return cookie.expires != null && cookie.expires!.isBefore(now);
    });
  }
}

class LoadingInterceptor extends InterceptorContract {
  final LoadingService _service;

  LoadingInterceptor(this._service);

  @override
  FutureOr<http.BaseRequest> interceptRequest(
      {required http.BaseRequest request}) {
    _service.setLoading(true);
    return request;
  }

  @override
  FutureOr<http.BaseResponse> interceptResponse(
      {required http.BaseResponse response}) {
    _service.setLoading(false);
    return response;
  }
}

class LoadingService extends ChangeNotifier {
  bool _loading = false;

  bool get isLoading => _loading;

  void setLoading(bool loading) {
    _loading = loading;
    notifyListeners();
  }
}

@riverpod
LoadingService loadingService(Ref ref) {
  return LoadingService();
}

@riverpod
Client client(Ref ref) {
  return InterceptedClient.build(interceptors: [
    CookieInterceptor(),
    LoadingInterceptor(ref.watch(loadingServiceProvider))
  ]);
}
