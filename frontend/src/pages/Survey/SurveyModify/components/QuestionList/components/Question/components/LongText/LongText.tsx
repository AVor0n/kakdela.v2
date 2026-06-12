import { TextArea } from '@hh.ru/magritte-ui';
import { useState } from 'react';
import './LongText.css';
export function LongText() {
    const [value, setValue] = useState('');
    return (
        <TextArea
            placeholder='Длинный текст'
            value={value}
            onChange={(e) => setValue(e.target.value)}
            size='large'
            layout='hug'
            disabled
        />
    );
}
