import { useAppSelector } from '@/hooks/useAppSelector';
import { Avatar, Button, Flex } from '@hh.ru/magritte-ui';
import { useEffect, useState } from 'react';
import { PopoverDetail } from './components/PopoverDetail/PopoverDetail';
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
            {!account?.hhSso && (
                <Button Element='a' href='/api/accounts/me/link-hh-sso/init' mode='primary' style='negative'>
                    <Flex align={'center'} gap='0.5rem'>
                        <p>Подключить</p> <img width={28} src='/hh_logo_white.png' alt='hh' />
                    </Flex>
                </Button>
            )}
            <Avatar
                Element='button'
                mode='letters'
                letters={letters}
                aria-label={account?.login ?? ''}
                style='color-9'
                size={48}
                disabled={false}
                online={false}
                shapeCircle={false}
                styleOnline='positive'
                video={false}
                onClick={() => setIsOpenModal(!isOpenModal)}
            />
            {isOpenModal && <PopoverDetail onMouseLeave={() => setIsOpenModal(false)} />}
        </div>
    );
}
