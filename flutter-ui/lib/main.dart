import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:quiz/routes.dart';

void main() {
  runApp(const ProviderScope(child: QuizApp()));
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
