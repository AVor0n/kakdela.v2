export type SurveyAnswerResponse = {
    responseId: string;
    questionId: string;
    answerText: string;
};

export type SurveyCompletedResponse = {
    id: string;
    account: ResponseAccountDetail | null;
    surveyId: string;
    isCompleted: boolean;
    receivedAt: string | null;
    answers: SurveyAnswerResponse[];
};

export type ResponseAccountDetail = {
    id: string;
    login: string;
    email: string;
};
