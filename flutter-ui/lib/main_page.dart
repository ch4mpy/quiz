import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:quiz/main.dart';
import 'package:quiz/user.dart';
import 'package:quiz/user_chip.dart';

class MainPage extends ConsumerStatefulWidget {
  MainPage({super.key});

  @override
  ConsumerState<MainPage> createState() => _MainPageState();
}

class _MainPageState extends ConsumerState<MainPage> {
  var isTabbarVisible = false;

  _MainPageState();

  @override
  Widget build(BuildContext context) {
    final userService = ref.watch(userServiceProvider);
    final user = userService.current;

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
                ? UserChip()
                : IconButton(
                    onPressed: userService.initiateAuthorizationCodeFlow,
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
