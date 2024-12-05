// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'http.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$backendHash() => r'dd43c09f0452c8a8929a69d53b28973e3ed35e8b';

/// See also [backend].
@ProviderFor(backend)
final backendProvider = AutoDisposeProvider<Dio>.internal(
  backend,
  name: r'backendProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$backendHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef BackendRef = AutoDisposeProviderRef<Dio>;
String _$loadingHash() => r'7862b8f101430cc355cf5b57b17da33eddbc01e7';

/// To know if something is loading: ref.watch(loadingProvider).isNotEmpty
///
/// Copied from [Loading].
@ProviderFor(Loading)
final loadingProvider =
    AutoDisposeNotifierProvider<Loading, Map<String, String>>.internal(
  Loading.new,
  name: r'loadingProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$loadingHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$Loading = AutoDisposeNotifier<Map<String, String>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package