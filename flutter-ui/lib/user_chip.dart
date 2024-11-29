import 'package:flutter/material.dart';
import 'package:quiz/main.dart';
import 'package:quiz/user.dart';

class UserChip extends StatelessWidget {
  const UserChip({
    super.key,
    required this.user,
    required this.appState,
  });

  final User user;
  final QuizAppState appState;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Icon(
          Icons.account_circle,
          semanticLabel: "user account",
        ),
        IconButton(
            onPressed: appState.logout,
            icon: const Icon(
              Icons.logout,
              semanticLabel: "logout",
            )),
      ],
    );
  }
}
