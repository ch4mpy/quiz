import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BFFApi, LoginOptionDto } from '@c4-soft/bff-api';
import { UsersApi } from '@c4-soft/quiz-api';
import { Subscription, interval } from 'rxjs';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';
import { Observable } from 'rxjs/internal/Observable';
import { lastValueFrom } from 'rxjs/internal/lastValueFrom';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private user$ = new BehaviorSubject<User>(User.ANONYMOUS);
  private refreshSub?: Subscription;

  constructor(private bffApi: BFFApi, private usersApi: UsersApi, private http: HttpClient) {
    this.refresh();
  }

  refresh(): void {
    this.refreshSub?.unsubscribe();
    this.usersApi.getMe().subscribe({
      next: (user) => {
        this.user$.next(
          user.username
            ? new User(user.username, user.roles || [])
            : User.ANONYMOUS
        );
        if (!!user.username) {
          const now = Date.now();
          const delay = (1000 * user.exp - now) * 0.8;
          if (delay > 2000) {
            this.refreshSub = interval(delay).subscribe(() => this.refresh());
          }
        }
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
    lastValueFrom(this.http.post('/logout', null, { observe: 'response' }))
      .then((resp) => {
        const logoutUri = resp.headers.get('Location');
        if (!!logoutUri) {
          window.location.href = logoutUri;
        }
      })
      .finally(() => {
        this.user$.next(User.ANONYMOUS);
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

  constructor(readonly name: string, readonly roles: string[]) {}

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

  get isTrainer(): boolean {
    return this.hasAnyRole('trainer');
  }

  get isModerator(): boolean {
    return this.hasAnyRole('moderator');
  }
}
