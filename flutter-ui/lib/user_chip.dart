import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:quiz/user.dart';

class UserChip extends ConsumerWidget {
  const UserChip({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userService = ref.watch(userServiceProvider);
    return Row(
      children: [
        const Icon(
          Icons.account_circle,
          semanticLabel: "user account",
        ),
        IconButton(
            onPressed: userService.logout,
            icon: const Icon(
              Icons.logout,
              semanticLabel: "logout",
            )),
      ],
    );
  }
}
