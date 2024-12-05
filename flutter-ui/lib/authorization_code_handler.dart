import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:quiz/routes.dart';
import 'package:quiz/user.dart';

class AuthorizationCodeHandler extends ConsumerStatefulWidget {
  final GoRouterState state;

  const AuthorizationCodeHandler({super.key, required this.state});

  @override
  ConsumerState<AuthorizationCodeHandler> createState() =>
      _AuthorizationCodeHandlerState();
}

class _AuthorizationCodeHandlerState
    extends ConsumerState<AuthorizationCodeHandler>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: Duration(seconds: 2),
    )..repeat();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    ref
        .watch(userServiceProvider.notifier)
        .forwardAuthorizationCode(widget.state.uri)
        .then((value) {
      ref.read(goRouterProvider).go('/');
    });
    return Scaffold(
      appBar: AppBar(
        title: const Text("Qu!z"),
        backgroundColor: theme.colorScheme.primary,
        foregroundColor: theme.colorScheme.onPrimary,
      ),
      body: Center(
        child: AnimatedBuilder(
          animation: _controller,
          builder: (BuildContext context, Widget? child) {
            return Transform.rotate(
              angle: _controller.value * 2 * math.pi,
              child: Image.asset('images/android-chrome-192x192.png'),
            );
          },
        ),
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
