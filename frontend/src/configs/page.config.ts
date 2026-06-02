export class Pages {
    static readonly ROOT: string = '/';
    static readonly AUTH: string = '/auth';

    static readonly AUTH_LOGIN: string = this.AUTH + '/login';
    static readonly AUTH_REGISTER: string = this.AUTH + '/register';

    static readonly SURVEYS: string = '/surveys';
    static readonly SURVEYS_VIEW: string = this.SURVEYS + '/view/:id';
    static readonly SURVEYS_EDIT: string = this.SURVEYS + '/edit/:id';
    static readonly SURVEYS_CREATE: string = this.SURVEYS + '/create';
}
