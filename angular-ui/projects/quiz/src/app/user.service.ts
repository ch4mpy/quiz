import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BFFApi } from '@c4-soft/bff-api';
import { UsersApi } from '@c4-soft/quiz-api';
import { LoginOptionDto } from 'projects/c4-soft/bff-api/model/loginOptionDto';
import { interval, Subscription } from 'rxjs';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';
import { Observable } from 'rxjs/internal/Observable';
import { lastValueFrom } from 'rxjs/internal/lastValueFrom';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private user$ = new BehaviorSubject<User>(User.ANONYMOUS);
  private refreshSub?: Subscription;
  private subject = webSocket('ws://mc-ch4mp.local/bff/user/whoami');

  constructor(
    private bffApi: BFFApi,
    private usersApi: UsersApi,
    private http: HttpClient
  ) {
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
          const delay = 3000;
          this.refreshSub = interval(delay).subscribe(() => this.refresh());
        }
      },
      error: (error) => {
        console.warn(error);
        this.user$.next(User.ANONYMOUS);
      },
    });
  }

  login(loginUri: string) {
    const currentRoute = location.toString();
    this.http
      .get(loginUri, {
        headers: {
          'X-RESPONSE-STATUS': '200',
          'X-POST-LOGIN-SUCCESS-URI': currentRoute,
          'X-POST-LOGIN-FAILURE-URI': currentRoute,
        },
        observe: 'response',
      })
      .subscribe((resp) => {
        const location = resp.headers.get('Location');
        if (!!location) {
          window.location.href = location;
        }
      });
  }

  async logout() {
    lastValueFrom(this.http.post('/bff/logout', null, { observe: 'response' }))
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
