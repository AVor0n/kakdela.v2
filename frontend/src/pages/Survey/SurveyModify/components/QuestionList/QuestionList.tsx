import type { Question } from '@/shared/types/Question.type';
import { Question as QuestionComponent } from './components/Question/Question';
import './QuestionList.css';
import { useState } from 'react';
interface Props {
    questions: Question[];
}

export function QuestionList({ questions }: Props) {
    const [selectedQuestionId, setSelectedQuestionId] = useState<string | null>(null);
    return (
        <div className='question__list'>
            {questions.map((question) => (
                <QuestionComponent
                    key={question.id}
                    question={question}
                    onClick={() => setSelectedQuestionId(question.id)}
                    isEditMode={selectedQuestionId === question.id}
                />
            ))}
        </div>
    );
}
