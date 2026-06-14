import type { Question } from '@/shared/types/Question.type';
import { Question as QuestionComponent } from './components/Question/Question';
import './QuestionList.css';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedQuestion } from '@/entities/Survey/Survey.slice';

interface Props {
    questions: Question[];
}

export function QuestionList({ questions }: Props) {
    const { selectedQuestion } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    return (
        <div className='question__list'>
            {questions.map((question) => (
                <QuestionComponent
                    key={question.id}
                    question={question}
                    onClick={() => dispatch(setSelectedQuestion({ question }))}
                    isEditMode={selectedQuestion?.id === question.id}
                />
            ))}
        </div>
    );
}
