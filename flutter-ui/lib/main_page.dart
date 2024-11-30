import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:quiz/main.dart';
import 'package:quiz/user_chip.dart';

class MainPage extends StatefulWidget {
  String path;
  MainPage({super.key, required this.path});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  var isTabbarVisible = false;

  _MainPageState();

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<QuizAppState>();
    final user = appState.user;

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

    final theme = Theme.of(context);

    return DefaultTabController(
      length: tabs.length,
      child: Scaffold(
        appBar: AppBar(
          title: const Text("Qu!z"),
          backgroundColor: theme.colorScheme.primary,
          foregroundColor: theme.colorScheme.onPrimary,
          actions: [
            if (tabs.length > 1)
              IconButton(
                icon: const Icon(
                  Icons.menu_sharp,
                  semanticLabel: "display tabs",
                ),
                onPressed: toggleTabbar,
              ),
            user.isAuthenticated()
                ? UserChip(user: user, appState: appState)
                : IconButton(
                    onPressed: appState.login,
                    icon: const Icon(
                      Icons.login,
                      semanticLabel: "login",
                    )),
          ],
          bottom: isTabbarVisible
              ? TabBar(tabs: tabs, labelColor: theme.colorScheme.onPrimary)
              : null,
        ),
        body: Center(
          child: Column(children: [
            Text(widget.path),
            if (user.isAuthenticated()) ...[
              if (user.isTrainer()) const Text("You are a trainer"),
              if (user.isModerator()) const Text("You are a moderator"),
            ] else
              const Text("You are not logged in"),
          ]),
        ),
      ),
    );
  }

  toggleTabbar() {
    setState(() {
      isTabbarVisible = !isTabbarVisible;
    });
  }
}
