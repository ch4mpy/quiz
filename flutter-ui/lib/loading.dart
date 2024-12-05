import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:uuid/uuid.dart';

part 'loading.g.dart';

/// To know if something is loading: ref.watch(loadingProvider).isNotEmpty
@Riverpod(keepAlive: true)
@riverpod
class Loading extends _$Loading {
  final _uuid = Uuid();

  @override
  Map<String, String> build() {
    return {};
  }

  /// info: some info about what starts loading
  /// returns: an ID to provide when calling stop
  String start(String info) {
    final id = _uuid.v1();
    state[id] = info;
    return id;
  }

  /// id: the ID returned by start
  /// returns: the info about what was loading, or nothing if the ID was invalid
  String? stop(String id) {
    return state.remove(id);
  }
}
