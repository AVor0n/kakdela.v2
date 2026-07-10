import { useAppSelector } from '@/hooks/useAppSelector';
import { Avatar } from '@hh.ru/magritte-ui';
import { useEffect, useState } from 'react';
import { ModalDetail } from './components/ModalDetail/ModalDetail';
import style from './AccountDetail.module.css';
function getLetters(value?: string): string {
    if (!value) return '';
    const strs = value.split(' ');
    const letter = strs.map((str) => str[0].toUpperCase()).join('');
    return letter;
}

export function AccountDetail() {
    const [letters, setLetters] = useState<string>('');
    const [isOpenModal, setIsOpenModal] = useState<boolean>();
    const { account } = useAppSelector((state) => state.account);
    useEffect(() => {
        if (!account) return;
        setLetters(getLetters(account.login));
    }, [account]);

    return (
        <div className={style.container}>
            <Avatar
                Element='button'
                mode='letters'
                letters={letters}
                aria-label='Борис Якубович'
                style='color-9'
                size={48}
                disabled={false}
                online={false}
                shapeCircle={false}
                styleOnline='positive'
                video={false}
                onClick={() => setIsOpenModal(!isOpenModal)}
            />
            {isOpenModal && <ModalDetail onMouseLeave={() => setIsOpenModal(false)} />}
        </div>
    );
}
