import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'package:quiz/main.dart';
import 'package:quiz/main_page.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'routes.g.dart';

@riverpod
GoRouter goRouter(Ref ref) {
  return GoRouter(
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => MainPage(),
      ),
      GoRoute(
        path: '/bff/login/oauth2/code/quiz-bff',
        builder: (context, state) => AuthorizationCodeHandler(state: state),
      ),
      GoRoute(
        path: '/ui',
        redirect: (context, state) => '/',
      ),
    ],
  );
}
