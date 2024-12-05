// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'loading.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$loadingHash() => r'd6a60e579ce5eeadd47680b864c2294a50769aec';

/// To know if something is loading: ref.watch(loadingProvider).isNotEmpty
///
/// Copied from [Loading].
@ProviderFor(Loading)
final loadingProvider = NotifierProvider<Loading, Map<String, String>>.internal(
  Loading.new,
  name: r'loadingProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$loadingHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$Loading = Notifier<Map<String, String>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
