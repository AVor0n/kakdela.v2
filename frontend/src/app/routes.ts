import { generatePath } from 'react-router-dom';

export const routePatterns = {
    root: '/',
    forgotPassword: '/forgot-password',
    verifyCode: '/verify-code/:email',
    resetPassword: '/reset-password',
    authCallback: 'auth/callback',
    notFound: '/not-found',
    auth: '/auth',
    authLogin: '/auth/login',
    authRegister: '/auth/register',
    surveys: '/surveys',
    surveysView: '/surveys/:id',
    surveyModify: '/surveys/:id/modify',
    surveyCreate: '/surveys/create',
} as const;

export const routes = {
    root: () => routePatterns.root,
    login: () => routePatterns.authLogin,
    register: () => routePatterns.authRegister,
    survey: () => routePatterns.surveys,
    forgotPassword: () => routePatterns.forgotPassword,
    surveyCreate: () => routePatterns.surveyCreate,
    surveyEdit: (id: string) => generatePath(routePatterns.surveyModify, { id }),
    surveyQuestions: (id: string) => `${generatePath(routePatterns.surveyModify, { id })}/questions`,
    surveyAnswers: (id: string) => `${generatePath(routePatterns.surveyModify, { id })}/answers`,
    surveyView: (id: string) => generatePath(routePatterns.surveysView, { id }),
    surveyPreview: (id: string) => `${generatePath(routePatterns.surveysView, { id })}?preview=true`,
    verifyCode: (email: string) => generatePath(routePatterns.verifyCode, { email }),
    resetPassword: (email: string, code: string) =>
        routePatterns.resetPassword + '?email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code),
    templateQuestion: (id: string) => `${generatePath(routePatterns.surveyModify, { id })}/questions?template=true`,
};
