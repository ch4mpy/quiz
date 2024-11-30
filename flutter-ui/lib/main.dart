import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:http_interceptor/http_interceptor.dart';
import 'package:provider/provider.dart';
import 'package:quiz/http.dart';
import 'package:quiz/main_page.dart';
import 'package:quiz/user.dart';
import 'package:url_launcher/url_launcher.dart';

final Client _client =
    InterceptedClient.build(interceptors: [CookieInterceptor()]);

void main() {
  runApp(MaterialApp.router(
      routerConfig: GoRouter(
    routes: [
      GoRoute(
        path: '/',
        builder: (_, __) => const QuizApp(),
        routes: [
          GoRoute(
            path: '/bff/login/oauth2/code/quiz-bff',
            builder: (context, state) => AuthorizationCodeHandler(state: state),
          ),
        ],
      ),
    ],
  )));
}

class AuthorizationCodeHandler extends StatelessWidget {
  final GoRouterState state;

  AuthorizationCodeHandler({super.key, required this.state}) {
    _client.get(state.uri, headers: {'X-Response-Status': '200'}).then((value) {
      value.headers.forEach((key, value) {
        print('$key: $value');
      });
      _client
          .get(Uri.parse('https://quiz.c4-soft.com/bff/v1/users/me'))
          .then((userResponse) {
        final user = jsonDecode(userResponse.body);
        if (user != null) {
          log(user.toString());
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Text('Loging in...');
  }
}

class QuizApp extends StatelessWidget {
  const QuizApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) => QuizAppState(),
      child: MaterialApp(
        title: 'Qu!z',
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0x003f51b5)),
        ),
        home: MainPage(path: '/'),
      ),
    );
  }
}

class QuizAppState extends ChangeNotifier {
  User user = User.anonymous();

  login() async {
    final opts = await getLoginOptions();
    final authorizationUri = opts.length == 1
        ? await getAuthorizationUri(opts.first.loginUri)
        : null;
    if (authorizationUri != null) {
      launchUrl(authorizationUri);
    }
    notifyListeners();
  }

  logout() {
    user = User.anonymous();
    notifyListeners();
  }

  Future<List<LoginOption>> getLoginOptions() async {
    final response = await _client
        .get(Uri.parse('https://quiz.c4-soft.com/bff/login-options'));
    if (response.statusCode != 200) {
      return [];
    }

    final opts = jsonDecode(response.body) as List<dynamic>;
    return opts
        .map((opt) {
          return opt as Map<String, dynamic>;
        })
        .map(LoginOption.fromJson)
        .toList();
  }

  Future<Uri?> getAuthorizationUri(String loginUri) async {
    final response = await _client
        .get(Uri.parse(loginUri), headers: {'X-Response-Status': '200'});
    final location = response.headers['location'];
    return location != null ? Uri.parse(location) : null;
  }
}

class LoginOption {
  final String label;
  final String loginUri;

  const LoginOption({
    required this.label,
    required this.loginUri,
  });

  factory LoginOption.fromJson(Map<String, dynamic> json) {
    return switch (json) {
      {
        'label': String label,
        'loginUri': String loginUri,
      } =>
        LoginOption(
          label: label,
          loginUri: loginUri,
        ),
      _ => throw const FormatException('Failed to parse LoginOption.'),
    };
  }
}
