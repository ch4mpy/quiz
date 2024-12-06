# Mobile UI

This projects contains the UI for Android and iOS. It is written with Flutter.

The Riverpod package works with some code generation. When modifying the source, we should be running:
```
dart run build_runner watch -d
```

## Security

The backend single entrypoint is an [OAuth2 BFF](https://www.baeldung.com/spring-cloud-gateway-bff-oauth2). This means that REST requests are authorized with session cookies.

The session cookie and CSRF token are handled by Dio request interceptors configured on the `backendProvider`. So, be sure to use that provider when requests require authorization.

As the backend is authenticating users with the OAuth2 authorization code flow, the app opens a browser for "login". Users are redirected back to the app using Android AppLink or iOS SmartLink intercepting the callback to the BFF with the authorization code. The app then forwards this callback URI using the `backendProvider` (and its associated cookie).

The `userServiceProvider` exposes the current user.

## LoadingProvider

To know if something is loading, we may `ref.watch(loadingProvider).isNotEmpty`. To list loading tasks info when debugging: `ref.read(loadingProvider).values`.

To notify other widgets when we start and stop a loading task, we may call `final loadingTaskId = ref.read(loadingProvider.notifier).start('Info about what is loading')` and then `ref.read(loadingProvider.notifier).stop(loadingTaskId)`. However, Dio interceptors already start and stop loading tasks with the target URI as info. So we should **manually start / stop loading tasks only when the `backendProvider` is not involved**.