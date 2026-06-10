import type { Question } from '../types/Question.type';
import type { Survey } from '../types/Survey.type';
import { mockQuestions } from './Questions.mock';

export const mockSurvey: Survey = {
    id: '1',
    authorId: '1',
    title: 'Survey Title',
    description: 'Survey Description',
    questions: mockQuestions.filter((question: Question) => question.surveyId === '1'), // Фильтруем вопросы, связанные с данным опросом
};
