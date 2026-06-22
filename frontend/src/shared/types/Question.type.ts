export type QuestionType = 'SHORT_TEXT' | 'LONG_TEXT' | 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE';

type BaseQuestion<T extends QuestionType> = {
    id: string;
    title: string;
    type: T;
    mandatory: boolean;
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
    answerOptions: string[];
    answerOptionOrder: null;
};

export type Question = SimpleQuestion | QuestionWithOptions;
