import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:quiz/http.dart';

class QuizLogo extends ConsumerStatefulWidget {
  const QuizLogo({super.key});

  @override
  ConsumerState<QuizLogo> createState() => _QuizLogoState();
}

class _QuizLogoState extends ConsumerState<QuizLogo>
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
    final isLoading = ref.watch(loadingProvider);
    final logo = Text('Qu!z');

    return AnimatedBuilder(
      animation: _controller,
      builder: (BuildContext context, Widget? child) {
        return isLoading.isNotEmpty
            ? Transform.rotate(
                angle: _controller.value * 2 * math.pi,
                child: logo,
              )
            : logo;
      },
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
