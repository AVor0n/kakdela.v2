import type { Question } from '@/shared/types/Question.type';
import { Question as QuestionComponent } from './components/Question/Question';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedQuestion } from '@/entities/Survey/Survey.slice';
import { useMemo } from 'react';

import style from './QuestionList.module.css';

interface Props {
    questions: Question[];
    pageNumber: number;
    pageIndex: number;
}

export function QuestionList({ questions, pageIndex }: Props) {
    const { selectedQuestion } = useAppSelector((state) => state.survey);
    const editQuestionId = useMemo(() => selectedQuestion?.id, [selectedQuestion]);
    const dispatch = useAppDispatch();
    return (
        <div className={style.container}>
            {questions.map((question) => (
                <QuestionComponent
                    key={question.id}
                    question={question}
                    onClick={() => dispatch(setSelectedQuestion({ question, pageIndex }))}
                    isEditMode={editQuestionId === question.id}
                />
            ))}
        </div>
    );
}
