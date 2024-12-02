import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http_interceptor/http_interceptor.dart';
import 'package:quiz/http.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:url_launcher/url_launcher.dart';

part 'user.g.dart';

class User {
  final String name;
  final List<String> roles;

  User(this.name, this.roles);

  User.anonymous()
      : name = "",
        roles = [];

  bool isAuthenticated() {
    return name.isNotEmpty;
  }

  bool hasAnyRole(List<String> roles) {
    for (var r in roles) {
      if (this.roles.contains(r)) {
        return true;
      }
    }
    return false;
  }

  bool isTrainer() {
    return hasAnyRole(["trainer"]);
  }

  bool isModerator() {
    return hasAnyRole(["moderator"]);
  }
}

class UserService extends ChangeNotifier {
  User current = User.anonymous();
  final Client _client =
      InterceptedClient.build(interceptors: [CookieInterceptor()]);

  /// Initiates the authorization code flow by fetching the login options and
  /// then opening the authorization URI in the browser.
  void initiateAuthorizationCodeFlow() async {
    final opts = await getLoginOptions();
    if (opts.isEmpty) {
      throw Exception('No login options available from the backend.');
    } else if (opts.length > 1) {
      throw Exception(
          "This application supports only one login option but the backend returned ${jsonEncode(opts)}");
    }

    final authorizationUri = await getAuthorizationUri(opts.first.loginUri);
    if (authorizationUri == null) {
      throw Exception('No authorization URI available from the backend.');
    }
    launchUrl(authorizationUri);
  }

  /// Forwards the authorization code to the backend and refreshes the user.
  /// Intended to be called by a deep-link handler, when handling the redirection from the authorization server.
  Future<User> forwardAuthorizationCode(Uri authorizationCodeUri) async {
    await _client
        .get(authorizationCodeUri, headers: {'X-Response-Status': '200'});
    return refresh();
  }

  Future<User> refresh() async {
    final userResponse = await _client
        .get(Uri.parse('https://quiz.c4-soft.com/bff/v1/users/me'));
    final user = jsonDecode(userResponse.body);
    if (user != null) {
      if (user['username'] != current.name || user['roles'] != current.roles) {
        current = User(user['username'], List<String>.from(user['roles']));
        notifyListeners();
      }
    } else if (current.name != "" || current.roles.isNotEmpty) {
      current = User.anonymous();
      notifyListeners();
    }
    return current;
  }

  Future<User> logout() async {
    await _client.post(Uri.parse('https://quiz.c4-soft.com/bff/logout'));
    current = User.anonymous();
    notifyListeners();
    return current;
  }

  Future<List<LoginOption>> getLoginOptions() async {
    final response = await _client
        .get(Uri.parse('https://quiz.c4-soft.com/bff/login-options'));
    if (response.statusCode != 200) {
      return [];
    }

    final opts = jsonDecode(response.body) as List<dynamic>;
    return opts
        .map((opt) {
          return opt as Map<String, dynamic>;
        })
        .map(LoginOption.fromJson)
        .toList();
  }

  Future<Uri?> getAuthorizationUri(String loginUri) async {
    final response = await _client
        .get(Uri.parse(loginUri), headers: {'X-Response-Status': '200'});
    final location = response.headers['location'];
    return location != null ? Uri.parse(location) : null;
  }
}

@riverpod
UserService userService(Ref ref) {
  return UserService();
}

class LoginOption {
  final String label;
  final String loginUri;

  const LoginOption({
    required this.label,
    required this.loginUri,
  });

  factory LoginOption.fromJson(Map<String, dynamic> json) {
    return switch (json) {
      {
        'label': String label,
        'loginUri': String loginUri,
      } =>
        LoginOption(
          label: label,
          loginUri: loginUri,
        ),
      _ => throw const FormatException('Failed to parse LoginOption.'),
    };
  }
}
