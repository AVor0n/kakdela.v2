import { generatePath } from 'react-router-dom';

export const routePatterns = {
    root: '/',
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
    surveyCreate: () => routePatterns.surveyCreate,
    surveyEdit: (id: string) => generatePath(routePatterns.surveyModify, { id }),
    surveyQuestions: (id: string) => `${generatePath(routePatterns.surveyModify, { id })}/questions`,
    surveyView: (id: string) => generatePath(routePatterns.surveysView, { id }),
    surveyPreview: (id: string) => `${generatePath(routePatterns.surveysView, { id })}?preview=true`,
};
