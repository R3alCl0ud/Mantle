package slimeknights.mantle.client.book;

import com.google.gson.Gson;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.*;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.gui.book.GuiBook;
import static slimeknights.mantle.client.book.ResourceHelper.setBookRoot;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public class BookLoader implements IResourceManagerReloadListener {

  /** GSON object to be used for book loading purposes */
  public static final Gson GSON = new Gson();

  /** Maps page content presets to names */
  private static final HashMap<String, Class<? extends PageContent>> typeToContentMap = new HashMap<>();

  /** Internal registry of all books for the purposes of the reloader, maps books to name */
  private static final HashMap<String, BookData> books = new HashMap<>();

  public BookLoader() {
    ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);

    registerPageType("blank", ContentBlank.class);
    registerPageType("text", ContentText.class);
    registerPageType("image", ContentImage.class);
    registerPageType("image with text below", ContentImageText.class);
    registerPageType("text with image below", ContentTextImage.class);
    registerPageType("text with left image etch", ContentTextLeftImage.class);
    registerPageType("text with right image etch", ContentTextRightImage.class);
  }

  /**
   * Registers a type of page prefabricate
   *
   * @param name  The name of the page type
   * @param clazz The PageContent class for this page type
   * @RecommendedInvoke init
   */
  public static void registerPageType(String name, Class<? extends PageContent> clazz) {
    if (typeToContentMap.containsKey(name))
      throw new IllegalArgumentException("Page type " + name + " already in use.");

    typeToContentMap.put(name, clazz);
  }

  /**
   * Gets a type of page prefabricate by name
   *
   * @param name The name of the page type
   * @return The class of the page type, ContentError.class if page type not registered
   */
  public static Class<? extends PageContent> getPageType(String name) {
    return typeToContentMap.getOrDefault(name, ContentError.class);
  }

  /**
   * Adds a book to the loader, and returns a reference object
   * Be warned that the returned BookData object is not immediately populated, and is instead populated when the resources are loaded/reloaded
   *
   * @param name     The name of the book, modid: will be automatically appended to the front of the name unless that is already added
   * @param location The location of the book folder, prefixed with the resource domain
   * @return The book object, not immediately populated
   */
  public static BookData registerBook(String name, String location) {
    BookData info = new BookData(location);

    books.put(name.contains(":") ? name : Loader.instance().activeModContainer().getModId() + ":" + name, info);

    return info;
  }

  /**
   * Returns the GuiScreen for the book
   *
   * @param name The name of the book, prefixed with modid:
   * @return The GuiScreen to open for the book
   */
  public static GuiBook getBookGui(String name) {
    return null;
  }

  /**
   * Reloads all the books, called when the resource manager reloads, such as when the resource pack or the language is changed
   */
  @Override
  public void onResourceManagerReload(IResourceManager resourceManager) {
    for (BookData book : books.values()) {
      setBookRoot(book.bookLocation);

      book.pageCount = book.cascadeLoad();
      book.fullPageCount = (int) Math.ceil((book.pageCount - 1) / 2F) + 1;
    }
  }
}