export type SurveyCompletedResponse = {
    id: string;
    account: ResponseAccountDetail | null;
    surveyId: string;
    isCompleted: boolean;
    receivedAt: string | null;
    answers: SurveyAnswerResponse[];
};

export type SurveyAnswerResponse = {
    answerAsString: string;
    booleanValue: boolean | null;
    dateValue: string | null;
    questionId: string;
    questionTextSnapshot: string;
    responseId: string;
    selectedAnswerOptions: AnswerOptionResponse[];
    textValue: string | null;
    timeValue: string | null;
};

export type AnswerOptionResponse = {
    id: string;
    answerOptionTextSnapshot: string;
};

export type ResponseAccountDetail = {
    id: string;
    login: string;
    email: string;
};
