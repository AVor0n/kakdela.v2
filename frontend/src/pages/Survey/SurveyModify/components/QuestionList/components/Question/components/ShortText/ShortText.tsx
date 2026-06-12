import { Input } from '@hh.ru/magritte-ui';
import { useState } from 'react';

export function ShortText() {
    const [value, setValue] = useState('');
    return <Input value={value} onChange={(e) => setValue(e)} placeholder='Короткий текст' disabled />;
}
