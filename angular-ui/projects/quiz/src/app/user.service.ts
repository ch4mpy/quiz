import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http'
import { BFFApi, LoginOptionDto } from '@c4-soft/bff-api';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';
import { lastValueFrom } from 'rxjs/internal/lastValueFrom';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private user$ = new BehaviorSubject<User>(User.ANONYMOUS);

  constructor(private bffApi: BFFApi, private http: HttpClient) {
    this.refresh();
  }

  refresh(): void {
    this.bffApi.getMe().subscribe({
      next: (user) => {
        console.info(user);
        this.user$.next(
          user.username
            ? new User(
                user.username,
                user.roles || []
              )
            : User.ANONYMOUS
        );
      },
      error: (error) => {
        console.warn(error);
        this.user$.next(User.ANONYMOUS);
      },
    });
  }

  login(loginUri: string) {
    window.location.href = loginUri;
  }

  async logout() {
    lastValueFrom(this.bffApi.logout('response')).then((resp) => {
      const logoutUri = resp.headers.get('location') || '';
      if (logoutUri) {
        window.location.href = logoutUri;
      }
    }).finally(() => {
      this.user$.next(User.ANONYMOUS)
    });
  }

  get loginOptions(): Observable<Array<LoginOptionDto>> {
    return this.bffApi.getLoginOptions();
  }

  get valueChanges(): Observable<User> {
    return this.user$;
  }

  get current(): User {
    return this.user$.value;
  }
}

export class User {
  static readonly ANONYMOUS = new User('', []);

  constructor(
    readonly name: string,
    readonly roles: string[]
  ) {}

  get isAuthenticated(): boolean {
    return !!this.name;
  }

  hasAnyRole(...roles: string[]): boolean {
    for (let r of roles) {
      if (this.roles.includes(r)) {
        return true;
      }
    }
    return false;
  }
}
