export type SurveySection = 'questions' | 'answers' | 'settings';

export type SurveyNavigationItem = {
    section: SurveySection;
    label: string;
    path: string;
    disabled: boolean;
    disabledTitle?: string;
    disabledAriaLabel?: string;
};
