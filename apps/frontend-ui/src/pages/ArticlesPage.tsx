import { Header } from "../components/layout/Header";
import { Articles } from "../components/articles/Articles";
import { Footer } from "../components/layout/Footer";

export const ArticlesPage = () => {
  return (
    <div className="min-h-screen bg-white flex flex-col">
      <Header />
      <main className="pt-16 flex-grow">
        <Articles />
      </main>
      <Footer />
    </div>
  );
};
