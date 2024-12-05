import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:uuid/uuid.dart';

part 'http.g.dart';

@riverpod
Dio backend(Ref ref) {
  final dio = Dio(BaseOptions(baseUrl: 'https://quiz.c4-soft.com'));
  dio.interceptors.addAll([
    CookieInterceptor(),
    LoadingInterceptor(ref.read(loadingProvider.notifier))
  ]);
  return dio;
}

class CookieInterceptor extends InterceptorsWrapper {
  final List<Cookie> _cookies = [];

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) {
    final String cookieHeader = _cookieHeader(options.uri);
    if (cookieHeader.isNotEmpty) {
      options.headers.update('cookie', (value) => "$value; $cookieHeader",
          ifAbsent: () => cookieHeader);
    }

    if (options.method == 'POST' ||
        options.method == 'PUT' ||
        options.method == 'PATCH' ||
        options.method == 'DELETE') {
      final csrfCookie = _cookies.firstWhere((cookie) {
        final domain = cookie.domain ?? '';
        return domain.isNotEmpty &&
            options.uri.host.contains(domain) &&
            cookie.name == 'XSRF-TOKEN';
      }, orElse: () => Cookie('XSRF-TOKEN', ''));
      if (csrfCookie.value.isNotEmpty) {
        options.headers['X-XSRF-TOKEN'] = csrfCookie.value;
      }
    }

    handler.next(options);
  }

  @override
  void onResponse(
    Response response,
    ResponseInterceptorHandler handler,
  ) {
    _cleanExpiredCookies();

    response.headers['set-cookie']
        ?.map(Cookie.fromSetCookieValue)
        .forEach((cookie) {
      if ((cookie.domain ?? '').isEmpty) {
        cookie.domain = response.requestOptions.uri.host;
      }
      _setOrReplace(cookie);
    });

    handler.next(response);
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

class LoadingInterceptor extends InterceptorsWrapper {
  static const _requestIdHeaderName = 'X-REQUEST-ID';
  final Loading loading;

  LoadingInterceptor(this.loading);

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) {
    options.headers[_requestIdHeaderName] =
        loading.start(options.uri.toString());
    handler.next(options);
  }

  @override
  void onResponse(
    Response response,
    ResponseInterceptorHandler handler,
  ) {
    loading.stop(response.requestOptions.headers[_requestIdHeaderName]);
    handler.next(response);
  }

  @override
  void onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) {
    loading.stop(err.requestOptions.headers[_requestIdHeaderName]);
    handler.next(err);
  }
}

/// To know if something is loading: ref.watch(loadingProvider).isNotEmpty
@riverpod
class Loading extends _$Loading {
  final _uuid = Uuid();

  @override
  Map<String, String> build() {
    return {};
  }

  /// info: some info about what starts loading
  /// returns: an ID to provide when calling stop
  String start(String info) {
    final id = _uuid.v1();
    state[id] = info;
    return id;
  }

  /// id: the ID returned by start
  /// returns: the info about what was loading, or nothing if the ID was invalid
  String? stop(String id) {
    return state.remove(id);
  }
}