import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:quiz/routes.dart';
import 'package:quiz/user.dart';
import 'dart:math' as math;

void main() {
  runApp(const ProviderScope(child: QuizApp()));
}

class AuthorizationCodeHandler extends ConsumerWidget {
  final GoRouterState state;

  const AuthorizationCodeHandler({super.key, required this.state});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref
        .watch(userServiceProvider.notifier)
        .forwardAuthorizationCode(state.uri)
        .then((value) {
      ref.read(goRouterProvider).go('/');
    });
    return Image(image: AssetImage('images/android-chrome-192x192.png'));
  }
}

class QuizApp extends ConsumerWidget {
  const QuizApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'Qu!z',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0x003f51b5)),
      ),
      routerConfig: ref.watch(goRouterProvider),
    );
  }
}