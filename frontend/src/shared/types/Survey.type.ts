import type { Question } from './Question.type';

export type Survey = {
    id: string;
    authorId: string;
    isAuthorizedOnly: boolean;
    closingPage: string | null;
    createdAt: string;
    title: string;
    description: string | null;
    pages: Page[];
    doNotify: boolean;
    expireAt: string | null;
    isLimitedToOneResponse: boolean;
    isPublished: boolean;
    isTemplate: boolean;
};

export type Page = {
    id: string;
    title: string;
    description: string | null;
    surveyId: string;
    serialNumber: number;
    questions: Question[];
};

export type SurveyListItem = Pick<Survey, 'id' | 'title' | 'description' | 'createdAt' | 'isPublished'>;
