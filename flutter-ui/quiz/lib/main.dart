import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:quiz/main_page.dart';
import 'package:quiz/user.dart';
import 'package:http/http.dart' as http;
import 'package:url_launcher/url_launcher.dart';

void main() {
  runApp(MaterialApp.router(routerConfig: router));
}

class QuizApp extends StatelessWidget {
  const QuizApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) => QuizAppState(),
      child: MaterialApp(
        title: 'Quiz',
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0x003f51b5)),
        ),
        home: const MainPage(),
      ),
    );
  }
}

final router = GoRouter(
  routes: [
    GoRoute(
      path: '/',
      builder: (_, __) => const QuizApp(),
      routes: [
        GoRoute(
          path: '/bff/login/oauth2/code/quiz-bff',
          builder: (_, __) => Scaffold(
            appBar: AppBar(title: const Text('Deep Link authorization-code')),
          ),
        ),
        GoRoute(
          path: '/ui',
          builder: (_, __) => Scaffold(
            appBar: AppBar(title: const Text('Deep Link UI')),
          ),
        ),
      ],
    ),
  ],
);

class QuizAppState extends ChangeNotifier {
  User user = User.anonymous();

  login() async {
    user = User("ch4mpy", ["trainer", "moderator"]);
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
    final response =
        await http.get(Uri.parse('http://desktop-ch4mp/bff/login-options'));
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
    final response = await http.get(Uri.parse(loginUri));
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
