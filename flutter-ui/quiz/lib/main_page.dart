import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:quiz/main.dart';
import 'package:quiz/user_chip.dart';

class MainPage extends StatelessWidget {
  const MainPage({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<QuizAppState>();
    final user = appState.user;

    final theme = Theme.of(context);

    final tabs = [
      const Tab(
        icon: Icon(Icons.list),
      )
    ];
    if (user.isTrainer()) {
      tabs.add(const Tab(
        icon: Icon(Icons.task_alt),
      ));
    }

    return DefaultTabController(
      length: tabs.length,
      child: Scaffold(
        appBar: AppBar(
          title: const Text("Quiz"),
          backgroundColor: theme.colorScheme.primary,
          foregroundColor: theme.colorScheme.onPrimary,
          actions: [
            user.isAuthenticated()
                ? UserChip(user: user, appState: appState)
                : IconButton(
                    onPressed: appState.login,
                    icon: const Icon(
                      Icons.login,
                      semanticLabel: "login",
                    )),
          ],
          bottom: TabBar(tabs: tabs, labelColor: theme.colorScheme.onPrimary),
        ),
        body: Column(
          children: [
            SafeArea(
              child: Center(
                child: Column(children: [
                  if (user.isAuthenticated()) ...[
                    if (user.isTrainer()) const Text("You are a trainer"),
                    if (user.isModerator()) const Text("You are a moderator"),
                  ] else
                    const Text("You are not logged in"),
                ]),
              ),
            )
          ],
        ),
      ),
    );
  }
}
