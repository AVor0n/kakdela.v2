import { Avatar } from '@hh.ru/magritte-ui';
import './Header.css';

export function Header() {
    return (
        <header className='header'>
            <span className='header__username'>username</span>
            <Avatar aria-label='avatar' mode='letters' letters='US' size={48} style='color-9' />
        </header>
    );
}
