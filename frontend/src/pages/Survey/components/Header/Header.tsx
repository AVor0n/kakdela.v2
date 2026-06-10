import './Header.css';

export function Header() {
    return (
        <header className='header'>
            <span className='header__username'>username</span>
            <img src='https://picsum.photos/200/300' alt='avatar' className='header__avatar' />
        </header>
    );
}
