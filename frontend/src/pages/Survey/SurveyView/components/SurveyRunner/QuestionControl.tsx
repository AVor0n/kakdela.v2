import { Checkbox, Input, Radio, Text, TextArea, TextAreaGrowLimiter } from '@hh.ru/magritte-ui';
import type { Question } from '@/shared/types/Question.type';
import { HTMLRender } from '@/shared/ui/HTMLRender/HTMLRender';
import choiceStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/Choice.module.css';
import optionStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/Choice/components/Option/Option.module.css';
import longTextStyle from '@/pages/Survey/SurveyModify/components/QuestionList/components/Question/components/LongText/LongText.module.css';
import style from './SurveyRunner.module.css';
import { OTHER_OPTION_VALUE, type AnswerValue } from './answerPayload';

type Props = {
    question: Question;
    value?: AnswerValue;
    otherText: string;
    disabled: boolean;
    onChange: (_value: AnswerValue) => void;
    onOtherTextChange: (_text: string) => void;
    onBlur: () => void;
};

function OtherOption({
    multiple,
    questionId,
    selected,
    text,
    disabled,
    onSelect,
    onTextChange,
}: {
    multiple: boolean;
    questionId: string;
    selected: boolean;
    text: string;
    disabled: boolean;
    onSelect: () => void;
    onTextChange: (_text: string) => void;
}) {
    const Control = multiple ? Checkbox : Radio;
    return (
        <div className={style.anotherOption}>
            <Control
                {...(!multiple ? { name: questionId } : {})}
                disabled={disabled}
                checked={selected}
                onChange={onSelect}
            />
            <p>Другое: </p>
            <input
                className={style.another}
                disabled={!selected || disabled}
                value={text}
                onChange={(event) => onTextChange(event.target.value)}
            />
        </div>
    );
}

export function QuestionControl({ question, value, otherText, disabled, onChange, onOtherTextChange, onBlur }: Props) {
    const toggleMultipleChoice = (optionId: string) => {
        const selectedOptions = Array.isArray(value) ? value : [];
        onChange(
            selectedOptions.includes(optionId)
                ? selectedOptions.filter((selectedOptionId) => selectedOptionId !== optionId)
                : [...selectedOptions, optionId],
        );
    };

    switch (question.type) {
        case 'SHORT_TEXT':
            return (
                <Input
                    placeholder='Короткий текст'
                    size='large'
                    disabled={disabled}
                    value={typeof value === 'string' ? value : ''}
                    onChange={onChange}
                    onBlur={onBlur}
                />
            );
        case 'LONG_TEXT':
            return (
                <TextAreaGrowLimiter className={longTextStyle.content}>
                    <TextArea
                        placeholder='Длинный текст'
                        disabled={disabled}
                        value={typeof value === 'string' ? value : ''}
                        onChange={(event) => onChange(event.target.value)}
                        onBlur={onBlur}
                        size='large'
                        layout='hug'
                    />
                </TextAreaGrowLimiter>
            );
        case 'SINGLE_CHOICE':
        case 'MULTIPLE_CHOICE': {
            const multiple = question.type === 'MULTIPLE_CHOICE';
            const selectedOptions = Array.isArray(value) ? value : [];
            const otherSelected = multiple
                ? selectedOptions.includes(OTHER_OPTION_VALUE)
                : value === OTHER_OPTION_VALUE;
            return (
                <div className={choiceStyle.container}>
                    {question.answerOptions.map((option) => (
                        <div className={optionStyle.optionContent} key={option.id}>
                            <label className={optionStyle.option}>
                                {multiple ? (
                                    <Checkbox
                                        disabled={disabled}
                                        checked={selectedOptions.includes(option.id)}
                                        onChange={() => toggleMultipleChoice(option.id)}
                                    />
                                ) : (
                                    <Radio
                                        name={question.id}
                                        disabled={disabled}
                                        checked={value === option.id}
                                        onChange={() => onChange(option.id)}
                                    />
                                )}
                                <Text typography='paragraph-2-regular' style='primary'>
                                    <HTMLRender html={option.text} />
                                </Text>
                            </label>
                        </div>
                    ))}
                    {question.hasOtherOption && (
                        <OtherOption
                            multiple={multiple}
                            questionId={question.id}
                            selected={otherSelected}
                            text={otherText}
                            disabled={disabled}
                            onSelect={() =>
                                multiple ? toggleMultipleChoice(OTHER_OPTION_VALUE) : onChange(OTHER_OPTION_VALUE)
                            }
                            onTextChange={onOtherTextChange}
                        />
                    )}
                </div>
            );
        }
        case 'YES_NO':
            return (
                <div className={choiceStyle.container}>
                    {([true, false] as const).map((booleanValue) => (
                        <label className={optionStyle.option} key={String(booleanValue)}>
                            <Radio
                                name={question.id}
                                disabled={disabled}
                                checked={value === booleanValue}
                                onChange={() => onChange(booleanValue)}
                            />
                            <Text typography='paragraph-2-regular' style='primary'>
                                {booleanValue ? 'Да' : 'Нет'}
                            </Text>
                        </label>
                    ))}
                </div>
            );
        case 'DATE':
        case 'TIME':
            return (
                <input
                    className={style.temporalInput}
                    type={question.type === 'DATE' ? 'date' : 'time'}
                    disabled={disabled}
                    value={typeof value === 'string' ? value : ''}
                    onChange={(event) => onChange(event.target.value)}
                />
            );
    }
}
