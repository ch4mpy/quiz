import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { UserService } from './user.service';

// FIXME: circular dependency
@Injectable()
export class UnauthorizedInterceptor implements HttpInterceptor {
  constructor(private user: UserService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err) => {
        //handle your auth error or rethrow
        if (err.status === 401) {
          this.user.refresh();
          this.user.loginOptions.subscribe({
            next: (opts) => {
              if (opts.length) {
                this.user.login(opts[0].loginUri);
              }
            },
          });
        }
        return throwError(() => err);
      })
    );
  }
}
