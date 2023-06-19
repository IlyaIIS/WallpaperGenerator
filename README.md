# WallpaperGenerator
Курсовой проект по разработке android приложения в Android studio с использованием языка Kotlin.

Выполнили: Исмаков Илья, Геер Максим 

### Использованный стек
- Retrofit - запросы к API
- Dagger - внедрение зависимостей
- JWT Token - авторизация
- SharedPreferences - сохранение пользовательских данных
- java.io - сохранение изображений на устройстве
- Использование многопоточности при генерации изображений
- Использование ViewModel

### Примечание
Для работы с API необходимо развернуть [бэкенд](https://github.com/MaximilianGeier/wallpaper-rest-api). *(на время с 19 по 20 числа бэкенд будет в рабочем состоянии)*

Билд приложения можно скачать [здесь](https://disk.yandex.ru/d/qGvqfd9fHE7YpA).

### Описание
Мобильный генератор обоев с возможностью их сохранения на устройство и добавления в общюю галерею, где можно просматривать и лайкать чужие изображения. 
Реализована система авторизации, собсвтенный бэкенд с 9 эндпоинтами и кастомный UI с использованием сторонних библиотек ([FKBlurView](https://github.com/furkankaplan/fk-blur-view-android)) и реализацией динамической подгрузки изображений, сортировки и параметров для генерации.

<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/de2021fc-1619-44c8-bcb8-e8cee856e200" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/23f5c6c8-8f90-4c62-a08b-4ac6f23eae8e" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/f9fc0e82-4948-473a-96eb-eb042dfe475c" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/0c53e374-744d-43ac-b49e-f8d95465d086" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/7de4e962-1096-4fee-9835-631bc81d4f48" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/bda582e6-f463-4510-848a-400aca9c590c" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/c1dc4516-a6b7-42b6-9a8a-0b49d295c13f" width="300">
<img src="https://github.com/IlyaIIS/WallpaperGenerator/assets/70832710/eef7b2e5-6a24-4f51-b747-95f79fc4e767" width="300">
