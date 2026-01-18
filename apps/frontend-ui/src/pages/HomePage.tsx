import { Header } from "../components/layout/Header";
import { Hero } from "../components/layout/Hero";
import { Features } from "../components/layout/Features";
import { Articles } from "../components/articles/Articles";
import { CTA } from "../components/layout/CTA";
import { Footer } from "../components/layout/Footer";

export const HomePage = () => {
  return (
    <div className="min-h-screen bg-white flex flex-col">
      <Header />
      <main className="flex-grow">
        <Hero />
        <Features />
        <Articles />
        <CTA />
      </main>
      <Footer />
    </div>
  );
};
