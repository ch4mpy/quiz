import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:quiz/main_page.dart';
import 'package:quiz/user.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'routes.g.dart';

@riverpod
GoRouter goRouter(Ref ref) {
  return GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => MainPage(),
      ),
      GoRoute(
        path: '/bff/login/oauth2/code/quiz-bff',
        redirect: (context, state) {
          ref
              .read(userServiceProvider.notifier)
              .forwardAuthorizationCode(state.uri);
          return '/';
        },
      ),
      GoRoute(
        path: '/ui',
        redirect: (context, state) => '/',
      ),
    ],
  );
}
