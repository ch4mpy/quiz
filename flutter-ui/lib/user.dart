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
