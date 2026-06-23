export type QuestionType = 'SHORT_TEXT' | 'LONG_TEXT' | 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE';

export type AnswerOption = {
    id: string;
    answerOptionText: string;
    serialNumber: number;
};
type BaseQuestion<T extends QuestionType> = {
    id: string;
    title: string;
    type: T;
    isMandatory: boolean;
    serialNumber: number;
    condition: null;
    description: null;
    visible: boolean;
};

type SimpleQuestion = BaseQuestion<'SHORT_TEXT' | 'LONG_TEXT'> & {
    answerOptions?: never;
    answerOptionOrder?: never;
};

type QuestionWithOptions = BaseQuestion<'SINGLE_CHOICE' | 'MULTIPLE_CHOICE'> & {
    answerOptions: AnswerOption[];
    answerOptionOrder: null;
};

export type Question = SimpleQuestion | QuestionWithOptions;
