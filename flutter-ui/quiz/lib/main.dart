import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:quiz/main_page.dart';
import 'package:quiz/user.dart';

void main() {
  runApp(const QuizApp());
}

class QuizApp extends StatelessWidget {
  const QuizApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) => QuizAppState(),
      child: MaterialApp(
        title: 'Quiz',
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0x003f51b5)),
        ),
        home: const MainPage(),
      ),
    );
  }
}

class QuizAppState extends ChangeNotifier {
  User user = User.anonymous();

  login() {
    user = User("ch4mpy", ["trainer", "moderator"]);
    notifyListeners();
  }

  logout() {
    user = User.anonymous();
    notifyListeners();
  }
}
