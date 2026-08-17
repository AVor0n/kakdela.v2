import type { Question } from './Question.type';
import type { Account } from './Account.type';
import type { Condition } from './Condition.type';

export type SurveyRole = 'AUTHOR' | 'EDITOR' | 'ANALYST';

export type ClosingPageFile = {
    fileName: string;
    fileSize: number;
};

export type ClosingPage = {
    title: string | null;
    description: string | null;
    attachmentUrl: string | null;
    websiteUrl: string | null;
    file: ClosingPageFile | null;
};

export type Survey = {
    id: string;
    author: Pick<Account, 'id' | 'login' | 'email'>;
    isAuthorizedOnly: boolean;
    closingPage: ClosingPage | null;
    createdAt: string;
    title: string;
    description: string | null;
    pages: Page[];
    doNotify: boolean;
    expireAt: string | null;
    isLimitedToOneResponse: boolean;
    isPublished: boolean;
    isTemplate: boolean;
    targetTimezone: string;
    expireAtAtTargetTimezone: string | null;
};

export type SurveyPageShort = Pick<Page, 'id' | 'serialNumber'>;

export type SurveyPublic = Pick<
    Survey,
    | 'id'
    | 'author'
    | 'title'
    | 'description'
    | 'isAuthorizedOnly'
    | 'isLimitedToOneResponse'
    | 'expireAt'
    | 'targetTimezone'
    | 'expireAtAtTargetTimezone'
> & {
    pages: SurveyPageShort[];
    hasCustomClosingPage: boolean;
    hasConditions: boolean;
};

export type Page = {
    id: string;
    title: string | null;
    description: string | null;
    surveyId: string;
    serialNumber: number;
    questions: Question[];
    conditions: Condition[];
};

export type SurveyPagePublic = Omit<Page, 'conditions'>;

export type SurveyListItem = Pick<Survey, 'id' | 'title' | 'description' | 'createdAt' | 'isPublished'> & {
    userRole: SurveyRole;
};

export type Template = Pick<Survey, 'id' | 'title' | 'description' | 'createdAt' | 'pages' | 'closingPage'> & {
    authorId: string;
    authorName: string;
    published: boolean;
};
export type TemplateListItem = Pick<Template, 'id' | 'title' | 'description' | 'createdAt'> & {
    published: boolean;
};
