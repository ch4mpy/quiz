import 'package:flutter/foundation.dart';

class User {
  User({required this.name, required this.roles});

  final String name;
  final List<String> roles;

  bool get isAuthenticated => name.isNotEmpty;

  static final User anonymous = User(name: '', roles: []);
}

class UserService with ChangeNotifier, DiagnosticableTreeMixin {
  User _user = User.anonymous;

  User get current => _user;

  void setUser(User user) {
    _user = user;
    notifyListeners();
  }

  /// Makes `UserService` readable inside the devtools by listing all of its properties
  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
        .add(StringProperty('current', '${current.name} ${current.roles}'));
  }
}
