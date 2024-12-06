import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:quiz/http.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:url_launcher/url_launcher.dart';

part 'user.g.dart';

@freezed
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

@Riverpod(keepAlive: true)
@riverpod
class UserService extends _$UserService {
  late Dio _backend;

  @override
  User build() {
    _backend = ref.read(backendProvider);
    return User.anonymous();
  }

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
    await _backend.get(authorizationCodeUri.toString(),
        options: Options(headers: {'X-Response-Status': 200}));
    return refresh();
  }

  Future<User> refresh() async {
    final userResponse = await _backend.get('/bff/v1/users/me');
    if (userResponse.data != null) {
      if (userResponse.data['username'] != state.name ||
          userResponse.data['roles'] != state.roles) {
        state = User(userResponse.data['username'],
            List<String>.from(userResponse.data['roles']));
      }
    } else if (state.name != "" || state.roles.isNotEmpty) {
      state = User.anonymous();
    }
    return state;
  }

  Future<User> logout() async {
    final rpLogout = await _backend.post('/bff/logout',
        options: Options(headers: {
          'X-POST-LOGOUT-SUCCESS-URI': '${_backend.options.baseUrl}/ui/'
        }));
    final locations = rpLogout.headers['location'];
    if (locations != null && locations.isNotEmpty && locations[0].isNotEmpty) {
      final opLogoutUrl = locations[0];
      state = User.anonymous();
      // A strict implementation would be
      // launchUrl(Uri.parse(opLogoutUrl));
      // But that causes the device to flicker, so we use an ajax request instead
      _backend.get(opLogoutUrl);
    }
    return state;
  }

  Future<List<LoginOption>> getLoginOptions() async {
    final response = await _backend.get('/bff/login-options');
    if (response.statusCode != 200) {
      return [];
    }
    return response.data
        .map((opt) => LoginOption.fromJson(opt))
        .toList()
        .cast<LoginOption>();
  }

  Future<Uri?> getAuthorizationUri(String loginUri) async {
    final response = await _backend.get(loginUri,
        options: Options(headers: {'X-Response-Status': '200'}));
    final locations = response.headers['location'];
    return locations != null && locations.isNotEmpty && locations[0].isNotEmpty
        ? Uri.parse(locations[0])
        : null;
  }
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
