import 'dart:async';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:http_interceptor/http_interceptor.dart';

class CookieInterceptor extends InterceptorContract {
  final Map<String, Cookie> _cookies = {};

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
      if (_cookies.containsKey('XSRF-TOKEN')) {
        request.headers['X-XSRF-TOKEN'] = _cookies['XSRF-TOKEN']!.value;
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
      _cookies[cookie.name] = cookie;
    });

    return response;
  }

  String _cookieHeader(Uri uri) {
    return _getSiteCookies(uri).map((cookie) {
      return "${cookie.name}=${cookie.value}";
    }).join("; ");
  }

  Iterable<Cookie> _getSiteCookies(Uri uri) {
    return _cookies.values.where((cookie) {
      if (cookie.sameSite == SameSite.none || cookie.domain == null) {
        return true;
      }
      return uri.host.endsWith(cookie.domain as String);
    });
  }

  void _cleanExpiredCookies() {
    final DateTime now = DateTime.now();
    _cookies.removeWhere((key, cookie) {
      return cookie.expires != null && cookie.expires!.isBefore(now);
    });
  }
}
