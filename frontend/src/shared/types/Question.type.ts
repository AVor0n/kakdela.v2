export type QuestionType = 'SHORT_TEXT' | 'LONG_TEXT' | 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE';
export type AnswerOptionOrder = 'ORIGINAL' | 'RANDOM';
export type AnswerOption = {
    id: string;
    text: string;
    serialNumber: number;
};
type BaseQuestion<T extends QuestionType> = {
    id: string;
    text: string;
    type: T;
    isMandatory: boolean;
    serialNumber: number;
    condition: null;
    description: string | null;
    visible?: boolean;
    isVisible?: boolean;
    attachmentUrl: string | null;
    hasOtherOption: boolean;
};

type SimpleQuestion = BaseQuestion<'SHORT_TEXT' | 'LONG_TEXT'> & {
    answerOptions?: never;
    answerOptionOrder?: never;
};

type QuestionWithOptions = BaseQuestion<'SINGLE_CHOICE' | 'MULTIPLE_CHOICE'> & {
    answerOptions: AnswerOption[];
    answerOptionOrder: AnswerOptionOrder | null;
};

export type Question = SimpleQuestion | QuestionWithOptions;
