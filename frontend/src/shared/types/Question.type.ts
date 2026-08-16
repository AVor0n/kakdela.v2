export type QuestionType =
    | 'SHORT_TEXT'
    | 'LONG_TEXT'
    | 'SINGLE_CHOICE'
    | 'MULTIPLE_CHOICE'
    | 'YES_NO'
    | 'DATE'
    | 'TIME';
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
    description: string | null;
    attachmentUrl: string | null;
    hasOtherOption: boolean;
};

type QuestionWithoutOptions = BaseQuestion<'SHORT_TEXT' | 'LONG_TEXT' | 'YES_NO' | 'DATE' | 'TIME'> & {
    answerOptions?: never;
    answerOptionOrder?: never;
};

type QuestionWithOptions = BaseQuestion<'SINGLE_CHOICE' | 'MULTIPLE_CHOICE'> & {
    answerOptions: AnswerOption[];
    answerOptionOrder: AnswerOptionOrder | null;
};

export type Question = QuestionWithoutOptions | QuestionWithOptions;
